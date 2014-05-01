/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package com.paphus.sdk;


/**
 * Exception class for th SDKConnection.
 * All exceptions will be thrown as SDKException runtime exceptions.
 */
public class SDKException extends RuntimeException {

	public SDKException(Exception exception) {
		super(exception.getMessage(), exception);
	}
	
	public SDKException(String message) {
		super(message);
	}
	
	public SDKException(String message, Exception exception) {
		super(message, exception);
	}
	
	public static SDKException parseFailure(Exception exception) {
		throw new SDKException("parse failure", exception);
	}
}