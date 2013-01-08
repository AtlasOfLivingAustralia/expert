package au.org.ala.expert

class ResultsCacheService {

    static cache = [:]

    def hasKey(key) {
        return cache.containsKey(key)
    }

    def get(key) {
        return cache[key]
    }

    def put(key, value) {
        cache.put key, value
    }

    def clear() {
        cache = [:]
    }
}
