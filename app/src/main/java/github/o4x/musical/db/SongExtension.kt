package github.o4x.musical.db

import github.o4x.musical.model.Song

fun Song.toHistoryEntity(timePlayed: Long): HistoryEntity {
    return HistoryEntity(
        id = id,
        timePlayed = timePlayed
    )
}

fun List<HistoryEntity>.historyToIds(): List<Long> {
    return map {
        it.id
    }
}

fun Song.toPlayCount(): PlayCountEntity {
    return PlayCountEntity(
        id = id,
        playCount = 1
    )
}

fun List<PlayCountEntity>.playCountToIds(): List<Long> {
    return map {
        it.id
    }
}

fun List<QueueEntity>.queueToIds(): List<Long> {
    return map {
        it.id
    }
}

fun Song.toQueueEntity(): QueueEntity {
    return QueueEntity(id = id)
}

fun List<Song>.toQueuesEntity(): List<QueueEntity> {
    return map {
        it.toQueueEntity()
    }
}

fun List<QueueOriginalEntity>.queueOriginalToIds(): List<Long> {
    return map {
        it.id
    }
}

fun Song.toQueueOriginalEntity(): QueueOriginalEntity {
    return QueueOriginalEntity(id = id)
}

fun List<Song>.toQueuesOriginalEntity(): List<QueueOriginalEntity> {
    return map {
        it.toQueueOriginalEntity()
    }
}

