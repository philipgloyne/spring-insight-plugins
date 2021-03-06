/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.struts2.test.action;

import com.opensymphony.xwork2.ActionSupport;
import com.springsource.insight.plugin.struts2.test.model.Person;

/**
 * Struts2 tests action with validation functionality
 * Acts as a controller to handle actions related to registering a user.
 *
 */
public class RegisterValidationAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	
	private Person personBean;
	
	
	public String execute() throws Exception {
		
		//call Service class to store personBean's state in database
		
		return SUCCESS;
		
	}
	
	public void validate(){
		
		if ( personBean.getFirstName() == null || personBean.getFirstName().length() == 0 ){	

			addFieldError( "personBean.firstName", "First name is required." );
			
		}
		
				
		if ( personBean.getEmail() == null || personBean.getEmail().length() == 0 ){	

			addFieldError( "personBean.email", "Email is required." );
			
		}
		
		if ( personBean.getAge() < 18 ){	

			addFieldError( "personBean.age", "Age is required and must be 18 or older" );
			
		}
		
		
	}

	
	public Person getPersonBean() {
		
		return personBean;
		
	}
	
	public void setPersonBean(Person person) {
		
		personBean = person;
		
	}

}
