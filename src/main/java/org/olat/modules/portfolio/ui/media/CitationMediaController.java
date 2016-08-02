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
package org.olat.modules.portfolio.ui.media;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Citation;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.manager.CitationXStream;
import org.olat.modules.portfolio.ui.component.CitationComponent;

/**
 * 
 * Initial date: 18.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CitationMediaController extends BasicController {
	
	public CitationMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("media_citation");
		mainVC.contextPut("author", "[author]");
		mainVC.contextPut("citation", media.getContent());
		putInitialPanel(mainVC);
		
		String citationXml = media.getMetadataXml();
		if(StringHelper.containsNonWhitespace(citationXml)) {
			Citation citation = (Citation)CitationXStream.get().fromXML(citationXml);
			CitationComponent cmp = new CitationComponent("cit");
			cmp.setCitation(citation);
			cmp.setDublinCoreMetadata(media);
			mainVC.put("cit", cmp);	
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}