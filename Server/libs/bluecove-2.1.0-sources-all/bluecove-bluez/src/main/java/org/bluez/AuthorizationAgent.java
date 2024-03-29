/**
 *  BlueCove - Java library for Bluetooth
 *  Copyright (C) 2007-2008 Vlad Skarzhevskyy
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *  @author vlads
 *  @version $Id: AuthorizationAgent.java 2471 2008-12-01 03:44:20Z skarzhevskyy $
 */
package org.bluez;

/**
 * Service unique name; Interface org.bluez.AuthorizationAgent; Object path
 * freely definable
 * 
 */
public interface AuthorizationAgent {
/*
	void Authorize(String adapter_path, String address,
			String service_path, String uuid)

	This method gets called when the service daemon wants
	to get an authorization for accessing a service. This
	method should return if the remote user is granted
	access or an error otherwise.

	The adapter_path parameter is the object path of the
	local adapter. The address, service_path and action
	parameters correspond to the remote device address,
	the object path of the service and the uuid of the
	profile.

	throws Error.Rejected
	                 Error.Canceled

void Cancel(String adapter_path, String address,
			String service_path, String uuid)

	This method cancels a previous authorization request.
	The adapter_path, address, service_path and uuid
	parameters must match the same values that have been
	used when the Authorize() method was called.

void Release()

	This method gets called when the service daemon
	unregisters an authorization agent. An agent can
	use it to do cleanup tasks. There is no need to
	unregister the agent, because when this method
	gets called it has already been unregistered.
*/	
}
