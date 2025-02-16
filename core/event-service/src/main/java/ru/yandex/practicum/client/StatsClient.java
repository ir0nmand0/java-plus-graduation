package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.SearchStats;
import ru.yandex.practicum.dto.CreateStatsDto;
import ru.yandex.practicum.dto.StatCountHitsDto;

import java.util.List;

@FeignClient(name = "stats-server")
public interface StatsClient {

    @PostMapping("${stats-server.hit}")
    void save(@RequestBody CreateStatsDto request);

    @GetMapping("${stats-server.stats}")
    List<StatCountHitsDto> get(@SpringQueryMap SearchStats searchStats);

}