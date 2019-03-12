package util

/**
 * Created by wyd on 2019/3/12 17:27:29.
 */
class TestUtil {
    static void waitResult(def result, long timeout) {
        int i = 0
        //!null = true
        while (!result && (i < timeout)) {
            println result
            sleep(1)
            i++
        }
    }
}
