/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2009 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jabsorb.serializer.impl;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Serialises date and time values
 */
public class DateStringSerializer extends AbstractSerializer
{

  private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class<?>[] _serializableClasses = new Class[] { Date.class,
      Timestamp.class, java.sql.Date.class, Time.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class<?>[] _JSONClasses = new Class[] { JSONObject.class };

  public Class<?>[] getJSONClasses()
  {
    return _JSONClasses;
  }

  public Class<?>[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Object marshall(SerializerState state, Object p, Object o)
      throws MarshallException
  {
    String timeString;
    if (o instanceof Date)
    {
      timeString = simpleDateFormat.format((Date) o);
    }
    else
    {
      throw new MarshallException("cannot marshall date using class "
          + o.getClass());
    }
    JSONObject obj = new JSONObject();
    marshallHints(obj,o);
    try
    {
      obj.put("time_str", timeString);
    }
    catch (JSONException e)
    {
      throw new MarshallException(e.getMessage(), e);
    }
    return obj;
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class<?> clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class;
    try
    {
      java_class = jso.getString(JSONSerializer.JAVA_CLASS_FIELD);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("no type hint", e);
    }
    if (java_class == null)
    {
      throw new UnmarshallException("no type hint");
    }
    if (!(java_class.equals("java.util.Date")) && 
        !(java_class.equals("java.sql.Timestamp")) &&
        !(java_class.equals("java.sql.Time")) &&
        !(java_class.equals("java.sql.Date")) )
    {
      throw new UnmarshallException("not a Date");
    }
    state.setSerialized(o, ObjectMatch.OKAY);
    return ObjectMatch.OKAY;
  }

  public Object unmarshall(SerializerState state, Class<?> clazz, Object o)
      throws UnmarshallException
  {
    Class<?> realClazz = clazz;
    JSONObject jso = (JSONObject) o;
    String timeStr;
    try
    {
      timeStr = jso.getString("time_str");
    }
    catch(JSONException e)
    {
      throw new UnmarshallException("Could not get the time in date serialiser", e);
    }
    if (jso.has(JSONSerializer.JAVA_CLASS_FIELD))
    {
      try
      {
        realClazz = Class.forName(jso.getString(JSONSerializer.JAVA_CLASS_FIELD));
      }
      catch (ClassNotFoundException e)
      {
        throw new UnmarshallException(e.getMessage(), e);
      }
      catch(JSONException e)
      {
        throw new UnmarshallException("Could not find javaClass", e);
      }
    }
    Object returnValue = null;
    try {
      if (Date.class.equals(realClazz)) {
        returnValue = simpleDateFormat.parse(timeStr);
      } else if (Timestamp.class.equals(realClazz)) {
        returnValue = new Timestamp(simpleDateFormat.parse(timeStr).getTime());
      } else if (java.sql.Date.class.equals(realClazz)) {
        returnValue = new java.sql.Date(simpleDateFormat.parse(timeStr).getTime());
      } else if (Time.class.equals(realClazz)) {
        returnValue = new Time(simpleDateFormat.parse(timeStr).getTime());
      }
    }
    catch (ParseException pe) {
      throw new UnmarshallException("cannot parse:"+timeStr);
    }

    if (returnValue == null)
    {
      throw new UnmarshallException("invalid class " + realClazz);
    }
    state.setSerialized(o, returnValue);
    return returnValue;
   }

}
