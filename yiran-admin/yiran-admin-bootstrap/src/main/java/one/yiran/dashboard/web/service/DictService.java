package one.yiran.dashboard.web.service;


import one.yiran.dashboard.manage.entity.SysDictData;
import one.yiran.dashboard.manage.service.SysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("dict")
public class DictService {
    @Autowired
    private SysDictDataService sysDictDataService;

    public List<SysDictData> getType(String dictType) {
        List<SysDictData> lists = sysDictDataService.selectNormalDictDataByType(dictType);
        lists = lists.stream().map(e->{
            e.setCreateTime(null);
            e.setUpdateTime(null);
            e.setCreateBy(null);
            e.setUpdateBy(null);
            return e;
        }).collect(Collectors.toList());
        return lists;
    }

    public String getLabel(String dictType, String dictValue) {
        return sysDictDataService.selectDictLabel(dictType, dictValue);
    }
}
