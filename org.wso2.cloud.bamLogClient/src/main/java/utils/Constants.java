/**
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils;

import java.io.File;

public class Constants {

    /**
     * Frequently used relative paths
     */
    public static final String LOGCLIENT_CONF_PATH = "logClient.conf";
    public static final String LOG4J_PROPERTY_PATH = "resources" + File.separator + "log4j.properties";
    public static final String TRUST_STORE_PATH = "resources" + File.separator + "client-truststore.jks";

    /**
     * Frequently used configuration tags
     */
    public static final String TRUST_STORE_PASSWORD = "wso2carbon";
}
