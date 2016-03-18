package org.olat.modules.video.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.video.managers.VideoManager;
import org.olat.modules.video.models.VideoQualityTableModel;
import org.olat.modules.video.models.VideoQualityTableModel.QualityTableCols;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;


public class VideoQualityTableFormController extends FormBasicController {

	private FlexiTableElement tableEl;
	private VideoQualityTableModel tableModel;

	@Autowired
	private VideoManager videoManager;
	private OLATResource videoResource;


	public VideoQualityTableFormController(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.videoResource = videoResource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("tab.video.qualityConfig"));
		generalCont.setRootForm(mainForm);
		generalCont.setFormContextHelp("Video Tracks");
		formLayout.add(generalCont);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.type.i18nKey(), QualityTableCols.type.ordinal(), true, QualityTableCols.type.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.dimension.i18nKey(), QualityTableCols.dimension.ordinal(), true, QualityTableCols.dimension.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.size.i18nKey(), QualityTableCols.size.ordinal(), true, QualityTableCols.size.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.format.i18nKey(), QualityTableCols.format.ordinal(), true, QualityTableCols.format.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.view.i18nKey(), QualityTableCols.view.ordinal(), true, QualityTableCols.view.name()));
		tableModel = new VideoQualityTableModel(columnsModel, getTranslator());

		List<QualityTableRow> rows = new ArrayList<QualityTableRow>();

		Size origSize = videoManager.getVideoSize(videoResource);

		FormLink viewButton = uifactory.addFormLink("view", "viewQuality", "quality.view", "qulaity.view", null, Link.LINK);
		rows.add(new QualityTableRow("original", origSize.getWidth() +"x"+ origSize.getHeight(),  FileUtils.byteCountToDisplaySize(videoManager.getVideoFile(videoResource).length()), "mp4",viewButton));
		tableModel.setObjects(rows);
		tableEl = uifactory.addTableElement(getWindowControl(), "qualities", tableModel, getTranslator(), generalCont);
		tableEl.setCustomizeColumns(false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

}