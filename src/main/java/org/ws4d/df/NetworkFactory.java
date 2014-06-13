/**
 * Copyright (C) 2014 PipesBox UG (haftungsbeschränkt) (elmar.zeeb@pipesbox.de)
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
package org.ws4d.df;

import org.ws4d.df.api.comp.type.ComponentTypeRegistry;
import org.ws4d.df.api.data.DataProvider;
import org.ws4d.df.api.network.ConnectionValidator;
import org.ws4d.df.api.network.NetworkBuilder;
import org.ws4d.df.impl.NetworkBuilderImpl;

public class NetworkFactory {

    // -------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------

    public static NetworkBuilder newNetworkBuilder(DataProvider dataProvider,
            ComponentTypeRegistry registry) {
        return newNetworkBuilder(dataProvider, registry, null);
    }

    public static NetworkBuilder newNetworkBuilder(DataProvider dataProvider,
            ComponentTypeRegistry registry, ConnectionValidator validator) {

        NetworkBuilder builder = new NetworkBuilderImpl();

        // data provider is mandatory
        if (dataProvider != null) {
            builder.setDataProvider(dataProvider);
        } else {
            return null;
        }

        // component registry is mandatory
        if (registry != null) {
            builder.setComponentTypeRegistry(registry);
        } else {
            return null;
        }

        // connection validator is optional
        if (validator != null) {
            builder.setConnectionValidator(validator);
        }

        return builder;
    }
}
