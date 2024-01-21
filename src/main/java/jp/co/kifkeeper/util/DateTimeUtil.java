package jp.co.kifkeeper.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtil {

	public static String getNowDateStr(String format) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return now.format(formatter);
	}

	public static String convertDateStrFormat(String inputDateStr, String inputFormat, String outputFormat) {
		String convertDataStr;
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
		SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
		try {
			Date inputDate = inputDateFormat.parse(inputDateStr);
			convertDataStr = outputDateFormat.format(inputDate);
			return convertDataStr;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
}
