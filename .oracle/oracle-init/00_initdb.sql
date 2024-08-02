alter database set TIME_ZONE='Europe/Oslo';
alter database datafile 1 autoextend on maxsize 2G;
alter system set recyclebin=OFF DEFERRED;
alter system set processes=150 scope=spfile;
alter system set session_cached_cursors=100 scope=spfile;
alter system set session_max_open_files=100 scope=spfile;
alter system set sessions=100 scope=spfile;
alter system set license_max_sessions=100 scope=spfile;
alter system set license_sessions_warning=100 scope=spfile;
alter system set disk_asynch_io=FALSE scope=spfile;
alter system set open_cursors=300 scope=both sid='*';
alter profile default limit password_life_time unlimited;
alter profile default limit password_verify_function null;
