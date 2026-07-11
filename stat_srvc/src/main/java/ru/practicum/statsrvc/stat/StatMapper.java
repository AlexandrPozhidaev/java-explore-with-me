package ru.practicum.statsrvc.stat;

import ru.practicum.dto.StatDto;

public class StatMapper {

    public static Stat toEntity(StatDto dto) {
        if (dto == null) {
            return null;
        }
        return new Stat(
                dto.getApp(),
                dto.getUri(),
                dto.getIp(),
                dto.getTimestamp()
        );
    }

    public static StatDto toDto(Stat entity) {
        if (entity == null) {
            return null;
        }
        return new StatDto(
                entity.getApp(),
                entity.getUri(),
                entity.getIp()
        );
    }
}
