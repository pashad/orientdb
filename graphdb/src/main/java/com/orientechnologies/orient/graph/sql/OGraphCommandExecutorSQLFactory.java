/*
 * Copyright 2012 Orient Technologies.
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
package com.orientechnologies.orient.graph.sql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.sql.OCommandExecutorSQLAbstract;
import com.orientechnologies.orient.core.sql.OCommandExecutorSQLFactory;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

/**
 * Graph related command operator executor factory. It's auto-discovered.
 * 
 * @author Luca Garulli
 */
public class OGraphCommandExecutorSQLFactory implements OCommandExecutorSQLFactory {

  private static final Map<String, Class<? extends OCommandExecutorSQLAbstract>> COMMANDS;

  static {

    // COMMANDS
    final Map<String, Class<? extends OCommandExecutorSQLAbstract>> commands = new HashMap<String, Class<? extends OCommandExecutorSQLAbstract>>();

    commands.put(OCommandExecutorSQLCreateEdge.NAME, OCommandExecutorSQLCreateEdge.class);
    commands.put(OCommandExecutorSQLDeleteEdge.NAME, OCommandExecutorSQLDeleteEdge.class);
    commands.put(OCommandExecutorSQLCreateVertex.NAME, OCommandExecutorSQLCreateVertex.class);
    commands.put(OCommandExecutorSQLDeleteVertex.NAME, OCommandExecutorSQLDeleteVertex.class);

    COMMANDS = Collections.unmodifiableMap(commands);
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getCommandNames() {
    return COMMANDS.keySet();
  }

  /**
   * {@inheritDoc}
   */
  public OCommandExecutorSQLAbstract createCommand(final String name) throws OCommandExecutionException {
    final Class<? extends OCommandExecutorSQLAbstract> clazz = COMMANDS.get(name);

    if (clazz == null) {
      throw new OCommandExecutionException("Unknowned command name :" + name);
    }

    try {
      return clazz.newInstance();
    } catch (Exception e) {
      throw new OCommandExecutionException("Error in creation of command " + name
          + "(). Probably there is not an empty constructor or the constructor generates errors", e);
    }
  }

  /**
   * Returns a OrientBaseGraph implementation from the current database in thread local.
   * 
   * @return
   */
  public static OrientBaseGraph getGraph() {
    ODatabaseRecord database = ODatabaseRecordThreadLocal.INSTANCE.get();
    if (!(database instanceof OGraphDatabase))
      database = new OGraphDatabase((ODatabaseRecordTx) database);

    final OrientGraphNoTx g = new OrientGraphNoTx((OGraphDatabase) database);
    g.setUseClassForEdgeLabel(true);
    return g;
  }
}
