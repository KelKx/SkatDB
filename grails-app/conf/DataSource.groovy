dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
        dataSource {
			dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
            dbCreate = "update"
            url = "jdbc:mysql://localhost/skatdb?useUnicode=yes&characterEncoding=UTF-8"
			driverClassName = "com.mysql.jdbc.Driver"
			username = "skatdb"
			password = "alleswirdgut"
            pooled = true
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=1800000
               timeBetweenEvictionRunsMillis=1800000
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               validationQuery="SELECT 1"
            }
        }
//		dataSource {
//			dbCreate = "update"
//			url = "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
//			pooled = true
//			properties {
//			   maxActive = -1
//			   minEvictableIdleTimeMillis=1800000
//			   timeBetweenEvictionRunsMillis=1800000
//			   numTestsPerEvictionRun=3
//			   testOnBorrow=true
//			   testWhileIdle=true
//			   testOnReturn=true
//			   validationQuery="SELECT 1"
//			}
//		}
    }
}