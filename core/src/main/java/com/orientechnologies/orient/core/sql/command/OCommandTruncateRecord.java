/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 * Copyright 2013 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.sql.command;

import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.orient.core.command.OCommandDistributedReplicateRequest;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.security.ODatabaseSecurityResources;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.parser.OSQLParser;
import com.orientechnologies.orient.core.version.OVersionFactory;
import static com.orientechnologies.orient.core.sql.parser.SQLGrammarUtils.*;

/**
 * SQL TRUNCATE RECORD command: Truncates a record without loading it. Useful when the record is dirty in any way and cannot be
 * loaded correctly.
 * 
 * @author Luca Garulli
 * @author Johann Sorel (Geomatys)
 * 
 */
public class OCommandTruncateRecord extends OCommandAbstract implements OCommandDistributedReplicateRequest{
  
  public static final String KEYWORD_TRUNCATE = "TRUNCATE";
  public static final String KEYWORD_RECORD = "RECORD";
  private Set<String> records = new HashSet<String>();
  
  public OCommandTruncateRecord() {
  }

  public OCommandTruncateRecord parse(final OCommandRequest iRequest) throws OCommandSQLParsingException {    
    final ODatabaseRecord database = getDatabase();
    database.checkSecurity(ODatabaseSecurityResources.COMMAND, ORole.PERMISSION_READ);

    final OSQLParser.CommandTruncateRecordContext candidate = getCommand(iRequest, OSQLParser.CommandTruncateRecordContext.class);
    
    if(candidate.orid() != null){
      records.add(candidate.orid().getText());
    }else if(candidate.collection() != null){
      final Collection col = (Collection) visit(candidate.collection()).evaluate(null, null);
      records.addAll(col);
    }
    return this;
  }

  /**
   * Execute the command.
   */
  public Object execute(final Map<Object, Object> iArgs) {
    if (records.isEmpty())
      throw new OCommandExecutionException("Cannot execute the command because it has not been parsed yet");

    final ODatabaseRecord database = getDatabase();
    for (String rec : records) {
      try {
        final ORecordId rid = new ORecordId(rec);
        database.getStorage().deleteRecord(rid, OVersionFactory.instance().createUntrackedVersion(), 0, null);
        database.getLevel1Cache().deleteRecord(rid);
      } catch (Throwable e) {
        throw new OCommandExecutionException("Error on executing command", e);
      }
    }

    return records.size();
  }

  @Override
  public String getSyntax() {
    return "TRUNCATE RECORD <rid>*";
  }
  
}
