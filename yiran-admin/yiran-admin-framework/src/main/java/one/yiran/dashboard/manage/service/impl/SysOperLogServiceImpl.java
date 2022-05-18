package one.yiran.dashboard.manage.service.impl;

import one.yiran.dashboard.manage.entity.SysOperLog;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.manage.service.SysOperLogService;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SysOperLogServiceImpl extends CrudBaseServiceImpl<Long, SysOperLog> implements SysOperLogService {

}
