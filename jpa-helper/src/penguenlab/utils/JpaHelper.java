
package penguenlab.utils;

/* ********************************************************************************************** *\
 * Project        : JpaHelper
 * Document       : JpaHelper.java (UTF-8)
 * Created on     : 12:26 28 July 2009
 * Authors        : Fırat KÜÇÜK
 * =================================================================================================
 * Jpa Helper Class
 * =================================================================================================
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 * Copyright (C) 2009, Fırat KÜÇÜK
 * ----------------
 * http://www.penguenyuvasi.org/ | firatkucuk_at_gmail_dot_com
 * TÜBİTAK MAM BTE, GEBZE / SAKARYA
 *
\* ********************************************************************************************** */



// ### REQUIRED CLASSES ############################################################################


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import javax.persistence.*;



// ### JpaHelper CLASS #############################################################################

public class JpaHelper {



  // ### FIELDS ####################################################################################

  private EntityManagerFactory emf;



  // ### METHODS ###################################################################################

  // +++ [JpaHelper] +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  public JpaHelper(String persistenceUnit) {
    emf = Persistence.createEntityManagerFactory(persistenceUnit);
  }



  // --- [getEntityManager] ------------------------------------------------------------------------

  public EntityManager getEntityManager() {
    return emf.createEntityManager();
  }



  // --- [create] ----------------------------------------------------------------------------------

  public Object create(Object entity) {
    final EntityManager em = getEntityManager();

    try {
      final EntityTransaction et = em.getTransaction();

      try {

        // Creating record
        et.begin();
        em.persist(entity);
        et.commit();

        // Searching for relations
        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field f : fields) {
          Annotation[] annotations = f.getAnnotations();

          for (Annotation a : annotations) {
            if (a.annotationType().equals(ManyToOne.class)) {
              Object     referenceEntity     = getter(entity, f).invoke(entity);
              Field      listField           = findOneToManyField(referenceEntity, f.getName());
              Method     getCollectionMethod = getter(referenceEntity, listField);
              Collection entityCollection    = (Collection) getCollectionMethod.invoke(referenceEntity);
              entityCollection.add(entity);

              et.begin();
              em.merge(referenceEntity);
              et.commit();
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (et != null && et.isActive()) {
          entity = null;
          et.rollback();
        }
      }
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }
    return entity;
  }



  // --- [update] ----------------------------------------------------------------------------------

  public Object update(Object entity) {
    final EntityManager em = getEntityManager();
    try {
      final EntityTransaction et = em.getTransaction();
      try {

        // Updating record
        et.begin();
        em.merge(entity);
        et.commit();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (et != null && et.isActive()) {
          entity = null;
          et.rollback();
        }
      }
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }
    return entity;
  }



  // --- [delete] ----------------------------------------------------------------------------------

  public void delete(Object entity) {
    final EntityManager em = getEntityManager();
    try {
      final EntityTransaction et = em.getTransaction();
      try {

        // Deleting record
        et.begin();
        em.remove(em.merge(entity));
        et.commit();

        // Searching for relations
        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field f : fields) {
          Annotation[] annotations = f.getAnnotations();

          for (Annotation a : annotations) {
            if (a.annotationType().equals(ManyToOne.class)) {
              Object     referenceEntity     = getter(entity, f).invoke(entity);
              Field      listField           = findOneToManyField(referenceEntity, f.getName());
              Method     getCollectionMethod = getter(referenceEntity, listField);
              Collection entityCollection    = (Collection) getCollectionMethod.invoke(referenceEntity);
              entityCollection.remove(entity);

              et.begin();
              em.merge(referenceEntity);
              et.commit();
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (et != null && et.isActive()) {
          entity = null;
          et.rollback();
        }
      }
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }
  }



  // --- [find] ------------------------------------------------------------------------------------

  public Object find(Class entity, Object primaryKey) {

    final EntityManager em = getEntityManager();
    Object result = null;

    try {
      result = em.find(entity, primaryKey);
    } catch (NoResultException nre) {
    } catch (NumberFormatException nre) {
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }

    return result;
  }



  // --- [getSpResults] ----------------------------------------------------------------------------

  public List getSpResults(String spName) {
    final EntityManager em = getEntityManager();
    List resultList = null;

    try {
      resultList = em.createNamedQuery(spName).getResultList();
    } catch (NoResultException nre) {
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }

    return resultList;
  }



  // --- [getSpResults] ----------------------------------------------------------------------------

  public List getSpResults(String spName, Object[]... parameters) {

    if (parameters.length == 0)
      return null;

    final EntityManager em = getEntityManager();
    List resultList = null;

    try {
      Query namedQuery = em.createNamedQuery(spName);

      for (Object[] p : parameters)
        namedQuery.setParameter(p[0].toString(), p[1]);

      resultList = namedQuery.getResultList();
    } catch (NoResultException nre) {
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }

    return resultList;
  }



  // --- [getSpResult] -----------------------------------------------------------------------------

  public Object getSpResult(String spName) {

    final EntityManager em = getEntityManager();
    Object result = null;

    try {
      result = em.createNamedQuery(spName).getSingleResult();
    } catch (NoResultException nre) {
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }

    return result;
  }



  // --- [getSpResult] -----------------------------------------------------------------------------

  public Object getSpResult(String spName, Object[]... parameters) {

    if (parameters.length == 0)
      return null;

    final EntityManager em = getEntityManager();
    Object result = null;

    try {

      Query namedQuery = getEntityManager().createNamedQuery(spName);

      for (Object[] p : parameters)
        namedQuery.setParameter(p[0].toString(), p[1]);

      result = namedQuery.getSingleResult();
    } catch (NoResultException nre) {
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }

    return result;
  }


  // --- [select] ----------------------------------------------------------------------------------

  public List select(String jpqlQuery) {
    final EntityManager em = getEntityManager();
    List resultList = null;

    try {
      Query query = em.createQuery(jpqlQuery);
      resultList = query.getResultList();
    } catch (NoResultException nre) {
    } finally {
      if (em != null && em.isOpen())
        em.close();
    }

    return resultList;
  }



  // --- [flush] -----------------------------------------------------------------------------------

  public void flush() {
    final EntityManager     em = getEntityManager();
    final EntityTransaction et = em.getTransaction();
    et.begin();
    em.flush();
    et.commit();
  }



  // --- [primaryKeyValue] -------------------------------------------------------------------------

  public static Object primaryKeyValue(Object entity) {

    Field[] fields = entity.getClass().getDeclaredFields();

    fieldsLoop:
    for (Field f : fields) {
      Annotation[] annotations = f.getAnnotations();

      for (Annotation a : annotations) {
        if (a.annotationType().equals(javax.persistence.Id.class)) {
          try {
            return getter(entity, f).invoke(entity);
          } catch (Exception e) {
            break fieldsLoop;
          }
        }
      }
    }

    return null;
  }



  // --- [getter] ----------------------------------------------------------------------------------

  private static Method getter(Object entity, Field field) {

    StringBuffer sbf = new StringBuffer(field.getName());
    String getterName = "get" + sbf.replace(0, 1, sbf.substring(0, 1).toUpperCase());

    try {
      return entity.getClass().getMethod(getterName);
    } catch (Exception e) {
      return null;
    }
  }



  // --- [setter] ----------------------------------------------------------------------------------

  private static Method setter(Object entity, Field field) {

    StringBuffer sbf = new StringBuffer(field.getName());
    String getterName = "set" + sbf.replace(0, 1, sbf.substring(0, 1).toUpperCase());

    try {
      return entity.getClass().getMethod(getterName);
    } catch (Exception e) {
      return null;
    }
  }



  // --- [findOneToManyField] ----------------------------------------------------------------------

  private static Field findOneToManyField(Object entity, String mappedBy) {

    Field[] fields = entity.getClass().getDeclaredFields();

    for (Field f : fields) {
      Annotation[] annotations = f.getAnnotations();
      for (Annotation a : annotations) {
        if (a.annotationType().equals(OneToMany.class) && ((OneToMany) a).mappedBy().equals(mappedBy))
          return f;
      }
    }

    return null;
  }
}