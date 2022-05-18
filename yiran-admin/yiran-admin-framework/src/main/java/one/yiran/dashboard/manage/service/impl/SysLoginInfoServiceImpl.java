package one.yiran.dashboard.manage.service.impl;

import one.yiran.dashboard.manage.entity.SysLoginInfo;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.manage.service.SysLoginInfoService;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SysLoginInfoServiceImpl extends CrudBaseServiceImpl<Long, SysLoginInfo> implements SysLoginInfoService {

}
