package one.yiran.dashboard.service.impl;

import one.yiran.dashboard.entity.SysOperateLog;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.service.SysOperLogService;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SysOperLogServiceImpl extends CrudBaseServiceImpl<Long, SysOperateLog> implements SysOperLogService {

}
