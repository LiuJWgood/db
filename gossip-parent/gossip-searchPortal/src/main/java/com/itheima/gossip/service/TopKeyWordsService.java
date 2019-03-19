package com.itheima.gossip.service;

import java.util.List;
import java.util.Map;

public interface TopKeyWordsService {

    public List<Map<String ,Object>> findByTopKeywords() throws Exception;
}
