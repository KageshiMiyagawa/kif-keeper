package jp.co.kifkeeper.util;

public class ExceptionUtil {

	public static void printRootCauseStackTrace(Throwable throwable) {
	    Throwable rootCause = getRootCause(throwable);
	    rootCause.printStackTrace();
	}

	public static Throwable getRootCause(Throwable throwable) {
	    Throwable cause = throwable.getCause();
	    if (cause != null) {
	        return getRootCause(cause);
	    }
	    return throwable;
	}

}
