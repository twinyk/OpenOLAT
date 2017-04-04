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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configure a course / repository entry for lecture.
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureRepositorySettingsController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	private static final String[] overrideKeys = new String[] { "yes", "no" };
	
	private SingleSelection overrideEl;
	private TextElement attendanceRateEl;
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement rollCallEnabledEl, calculateAttendanceRateEl;
	private MultipleSelectionElement teacherCalendarSyncEl, participantCalendarSyncEl;
	
	private boolean overrideModuleDefaults = false;
	private RepositoryEntryLectureConfiguration lectureConfig;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public LectureRepositorySettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		
		lectureConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		overrideModuleDefaults = lectureConfig.isOverrideModuleDefault();
		initForm(ureq);
		updateOverride();
		updateVisibility();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("lecture.admin.title");
		
		enableEl = uifactory.addCheckboxesHorizontal("lecture.admin.enabled", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureConfig.isLectureEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		String[] overrideValues = new String[]{ translate("config.override.yes"), translate("config.override.no") };
		overrideEl = uifactory.addRadiosHorizontal("config.override", formLayout, overrideKeys, overrideValues);
		overrideEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureConfig.isOverrideModuleDefault()) {
			overrideEl.select(overrideKeys[0], true);//yes
		} else {
			overrideEl.select(overrideKeys[1], true);//no
		}
		
		rollCallEnabledEl = uifactory.addCheckboxesHorizontal("config.rollcall.enabled", formLayout, onKeys, onValues);
		rollCallEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		calculateAttendanceRateEl = uifactory.addCheckboxesHorizontal("config.calculate.attendance.rate", formLayout, onKeys, onValues);
		attendanceRateEl = uifactory.addTextElement("lecture.attendance.rate.default", "lecture.attendance.rate.default", 4, "", formLayout);
		teacherCalendarSyncEl = uifactory.addCheckboxesHorizontal("config.sync.teacher.calendar", formLayout, onKeys, onValues);
		participantCalendarSyncEl = uifactory.addCheckboxesHorizontal("config.sync.participant.calendar", formLayout, onKeys, onValues);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateOverride() {
		updateOverrideElement(rollCallEnabledEl, lectureConfig.getRollCallEnabled(), lectureModule.isRollCallDefaultEnabled());
		updateOverrideElement(calculateAttendanceRateEl, lectureConfig.getCalculateAttendanceRate(), lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled());
		updateOverrideElement(teacherCalendarSyncEl, lectureConfig.getTeacherCalendarSyncEnabled(), lectureModule.isTeacherCalendarSyncEnabledDefault());
		updateOverrideElement(participantCalendarSyncEl, lectureConfig.getParticipantCalendarSyncEnabled(), lectureModule.isParticipantCalendarSyncEnabledDefault());		
	
		double attendanceRate;
		if(!overrideModuleDefaults || lectureConfig.getRequiredAttendanceRate() == null) {
			attendanceRate = lectureModule.getRequiredAttendanceRateDefault();
		} else {
			attendanceRate = lectureConfig.getRequiredAttendanceRate().doubleValue();
		}
		long attendanceRatePerCent = Math.round(attendanceRate * 100.0d);
		attendanceRateEl.setValue(Long.toString(attendanceRatePerCent));
		attendanceRateEl.setEnabled(overrideModuleDefaults);
	}
	
	private void updateOverrideElement(MultipleSelectionElement el, Boolean entryConfig, boolean defaultValue) {
		boolean enable = overrideModuleDefaults ? (entryConfig == null ? defaultValue : entryConfig.booleanValue()) : defaultValue ;
		if(enable) {
			el.select(onKeys[0], true);
		} else {
			el.uncheckAll();
		}
		el.setEnabled(overrideModuleDefaults);
	}
	
	private void updateVisibility() {
		boolean lectureEnabled = enableEl.isAtLeastSelected(1);
		boolean rollCallEnabled = rollCallEnabledEl.isAtLeastSelected(1);
		overrideEl.setVisible(lectureEnabled);
		rollCallEnabledEl.setVisible(lectureEnabled);
		calculateAttendanceRateEl.setVisible(lectureEnabled && rollCallEnabled);
		attendanceRateEl.setVisible(lectureEnabled && rollCallEnabled);
		teacherCalendarSyncEl.setVisible(lectureEnabled);
		participantCalendarSyncEl.setVisible(lectureEnabled);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(overrideEl == source) {
			if(overrideEl.isOneSelected()) {
				overrideModuleDefaults = overrideEl.isSelected(0);
				updateOverride();
			}
		} else if(enableEl == source || rollCallEnabledEl.isAtLeastSelected(1)) {
			updateVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		overrideEl.clearError();
		if(!overrideEl.isOneSelected()) {
			overrideEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		//override -> check rate
		attendanceRateEl.clearError();
		if(overrideEl.isSelected(0)) {
			if(rollCallEnabledEl.isAtLeastSelected(1) && calculateAttendanceRateEl.isAtLeastSelected(1)) {
				if(StringHelper.containsNonWhitespace(attendanceRateEl.getValue())) {
					try {
						long rateInPercent = Long.parseLong(attendanceRateEl.getValue());
						if(rateInPercent <= 0 || rateInPercent > 100) {
							attendanceRateEl.setErrorKey("error.integer.between", new String[] {"1", "100"});
							allOk &= false;
						}
					} catch(NumberFormatException e) {
						attendanceRateEl.setErrorKey("form.error.nointeger", null);
						allOk &= false;
					}
				} else {
					attendanceRateEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		lectureConfig.setLectureEnabled(enableEl.isAtLeastSelected(1));
		lectureConfig.setOverrideModuleDefault(overrideEl.isSelected(0));
		if(enableEl.isAtLeastSelected(1) && overrideEl.isSelected(0)) {
			lectureConfig.setRollCallEnabled(rollCallEnabledEl.isAtLeastSelected(1));
			
			//reset values
			lectureConfig.setCalculateAttendanceRate(null);
			lectureConfig.setRequiredAttendanceRate(null);
			if(rollCallEnabledEl.isAtLeastSelected(1)) {
				lectureConfig.setCalculateAttendanceRate(calculateAttendanceRateEl.isAtLeastSelected(1));
				if(calculateAttendanceRateEl.isAtLeastSelected(1)) {
					try {
						long rateInPercent = Long.parseLong(attendanceRateEl.getValue());
						double rate = rateInPercent <= 0 ? 0.0d : rateInPercent / 100.0d;
						lectureConfig.setRequiredAttendanceRate(rate);
					} catch (NumberFormatException e) {
						logError("", e);
					}
				}
			}
			lectureConfig.setTeacherCalendarSyncEnabled(teacherCalendarSyncEl.isAtLeastSelected(1));
			lectureConfig.setParticipantCalendarSyncEnabled(participantCalendarSyncEl.isAtLeastSelected(1));
		} else {
			lectureConfig.setRollCallEnabled(null);
			lectureConfig.setCalculateAttendanceRate(null);
			lectureConfig.setRequiredAttendanceRate(null);
			lectureConfig.setTeacherCalendarSyncEnabled(null);
			lectureConfig.setParticipantCalendarSyncEnabled(null);
		}
		
		lectureConfig = lectureService.updateRepositoryEntryLectureConfiguration(lectureConfig);
	}
}