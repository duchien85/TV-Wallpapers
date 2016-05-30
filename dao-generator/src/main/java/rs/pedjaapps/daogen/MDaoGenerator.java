/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rs.pedjaapps.daogen;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class MDaoGenerator
{
    public static void main(String[] args) throws Exception
    {
        Schema schema = new Schema(1, "rs.pedjaapps.mpviewallpapers.model");

        addUser(schema);
        addStat(schema);

        new DaoGenerator().generateAll(schema, "../app/src/main/java");
    }

    private static void addUser(Schema schema)
    {
        Entity user = schema.addEntity("User");
        user.setHasKeepSections(true);
        user.addStringProperty("username").notNull().primaryKey();
        user.addStringProperty("password");
        user.addStringProperty("gender");
        user.addIntProperty("weight_kg").notNull();
        user.addIntProperty("height_cm").notNull();
        user.addIntProperty("age").notNull();
    }

    private static void addStat(Schema schema)
    {
        Entity stat = schema.addEntity("Stat");
        stat.addIdProperty();
        stat.addStringProperty("type").notNull();
        stat.addLongProperty("time").notNull();
        stat.addIntProperty("data_int_1");
        stat.addIntProperty("data_int_2");
        stat.addStringProperty("data_string_1");
        stat.addStringProperty("data_string_2");
    }

}
