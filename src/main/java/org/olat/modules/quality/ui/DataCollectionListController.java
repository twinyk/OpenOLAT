/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.DataCollectionDataModel.DataCollectionCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionListController extends FormBasicController implements TooledController, Activateable2 {

	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private final TooledStackedPanel stackPanel;
	private Link createDataCollectionLink;
	private DataCollectionDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController formSearchCtrl;
	private DataCollectionDeleteConfirmationController deleteConfirmationCtrl;
	private DataCollectionController dataCollectionCtrl;
	
	private final QualitySecurityCallback secCallback;
	
	@Autowired
	private QualityService qualityService;

	public DataCollectionListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QualitySecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.secCallback = secCallback;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.deadline));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.topicType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.topic));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.formName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.numberParticipants));
		if (secCallback.canEditDataCollections()) {
			DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(DataCollectionCols.edit.i18nHeaderKey(),
					DataCollectionCols.edit.ordinal(), CMD_EDIT,
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer("", CMD_EDIT, "o_icon o_icon-lg o_icon_qual_dc_edit", null),
							null));
			editColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(editColumn);
		}
		if (secCallback.canDeleteDataCollections()) {
			DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(DataCollectionCols.delete.i18nHeaderKey(),
					DataCollectionCols.delete.ordinal(), CMD_DELETE,
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer("", CMD_DELETE, "o_icon o_icon-lg o_icon_qual_dc_delete", null),
							null));
			editColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(editColumn);
		}
		
		DataCollectionDataSource dataSource = new DataCollectionDataSource(getTranslator());
		dataModel = new DataCollectionDataModel(dataSource, columnsModel, getLocale(), secCallback);
		tableEl = uifactory.addTableElement(getWindowControl(), "dataCollections", dataModel, 25, true, getTranslator(), formLayout);
	}

	@Override
	public void initTools() {
		if (secCallback.canEditDataCollections()) {
			createDataCollectionLink = LinkFactory.createToolLink("data.collection.create", translate("data.collection.create"), this);
			createDataCollectionLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_dc_create");
			stackPanel.addTool(createDataCollectionLink, Align.left);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if (QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME.equals(type)) {
			Long key = entry.getOLATResourceable().getResourceableId();
			DataCollectionRow row = dataModel.getObjectByKey(key);
			if (row == null) {
				dataModel.load(null, null, null, 0, -1);
				row = dataModel.getObjectByKey(key);
				if (row != null) {
					doEditDataCollection(ureq, row.getDataCollection());
					int index = dataModel.getObjects().indexOf(row);
					if (index >= 1 && tableEl.getPageSize() > 1) {
						int page = index / tableEl.getPageSize();
						tableEl.setPage(page);
					}
				}
			} else {
				doEditDataCollection(ureq, row.getDataCollection());
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				DataCollectionRow row = dataModel.getObject(se.getIndex());
				if (CMD_EDIT.equals(cmd)) {
					doEditDataCollection(ureq, row.getDataCollection());
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDeleteDataCollection(ureq, row.getDataCollection());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(createDataCollectionLink == source) {
			doSelectEvaluationForm(ureq);
		} else if (stackPanel == source) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == this) {
					tableEl.reset(true, false, true);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == formSearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry formEntry = formSearchCtrl.getSelectedEntry();
				doCreateDataCollection(ureq, formEntry);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == deleteConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				QualityDataCollectionLight dataCollection = deleteConfirmationCtrl.getDataCollection();
				doDeleteDataCollection(dataCollection);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(formSearchCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		formSearchCtrl = null;
		cmc = null;
	}

	private void doSelectEvaluationForm(UserRequest ureq) {
		formSearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				EvaluationFormResource.TYPE_NAME, translate("data.collection.form.select"));
		this.listenTo(formSearchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				formSearchCtrl.getInitialComponent(), true, translate("data.collection.form.select"));
		cmc.activate();
	}
	
	private void doCreateDataCollection(UserRequest ureq, RepositoryEntry formEntry) {
		QualityDataCollection dataCollection = qualityService.createDataCollection(formEntry);
		doEditDataCollection(ureq, dataCollection);
	}
	
	private void doEditDataCollection(UserRequest ureq, QualityDataCollectionLight dataCollection) {
		WindowControl bwControl = addToHistory(ureq, dataCollection, null);
		dataCollectionCtrl = new DataCollectionController(ureq, bwControl, secCallback, stackPanel,
				dataCollection);
		String title = dataCollection.getTitle();
		stackPanel.pushController(Formatter.truncate(title, 50), dataCollectionCtrl);
		dataCollectionCtrl.activate(ureq, null, null);
	}

	private void doConfirmDeleteDataCollection(UserRequest ureq, QualityDataCollectionLight dataCollection) {
		deleteConfirmationCtrl = new DataCollectionDeleteConfirmationController(ureq, getWindowControl(), dataCollection);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				deleteConfirmationCtrl.getInitialComponent(), true, translate("data.collection.delete.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doDeleteDataCollection(QualityDataCollectionLight dataCollection) {
		qualityService.deleteDataCollection(dataCollection);
		tableEl.reset(true, false, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

}
