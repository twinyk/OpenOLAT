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
package org.olat.modules.qpool;

import java.util.List;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionPoolService {
	
	public List<Pool> getPools(Identity identity);
	

	public int getNumOfItemsInPool(Pool pool);
	
	public List<QuestionItem> getItemsOfPool(Pool pool, int firstResult, int maxResults);
	
	public int getNumOfFavoritItems(Identity identity);
	
	public List<QuestionItem> getFavoritItems(Identity identity, int firstResult, int maxResults);
	
	public List<QuestionItem> getItems(Identity identity, int firstResult, int maxResults);
	
	

}
