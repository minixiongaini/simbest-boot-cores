package com.simbest.boot.security.license;

import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.util.encrypt.Base64Encryptor;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.BootAppFileReader;
import com.simbest.boot.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Title : VerifyLicense.java</strong><br>
 * <strong>Description : </strong><br>
 * <strong>Create on : 2020年7月1日下午7:05:31</strong><br>
 * <strong>Modify on : 2020年7月1日下午7:05:31</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 * 
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 *          <strong>修改历史:</strong><br>
 *          修改人 修改日期 修改描述<br>
 *          -------------------------------------------<br>
 */
@Slf4j
@Component
public class VerifyLicense {

	@Autowired
	private RsaEncryptor rsaEncryptor;

	@Autowired
	private Base64Encryptor base64Encryptor;

	@Autowired
	private AppConfig appConfig;

	public boolean vertify() {
		boolean flag = Boolean.FALSE;
		try {
			if (StrUtil.equals(appConfig.getLicenseKeyCon(),"H") ){
				LocalDateTime keyDateTime = getLicenseDateTime();
				if (StrUtil.isBlankIfStr(keyDateTime)){
					return Boolean.TRUE;
				}
				flag = DateUtil.localDateTimeIsBefore(keyDateTime,LocalDateTime.now()) || DateUtil.localDateTimeIsEqual(keyDateTime,LocalDateTime.now());
				//log.warn("VerifyLicense================1>>>>>>{}",keyDateTime.toString());
				//log.warn("VerifyLicense================2>>>>>>{}",LocalDateTime.now().toString());
				//log.warn("VerifyLicense================3>>>>>>{}",DateUtil.localDateTimeIsBefore(keyDateTime,LocalDateTime.now()));
			}else {
				LocalDate keyDate = getLicenseDate();
				if (StrUtil.isBlankIfStr(keyDate)){
					return Boolean.TRUE;
				}
				flag = DateUtil.localDateIsBefore(keyDate,LocalDate.now()) || DateUtil.localDateIsEqual(keyDate,LocalDate.now());
			}
			return flag;
		} catch (Exception e) {
			Exceptions.printException(e);
			return flag;
		}
	}

	private LocalDateTime getLicenseDateTime(){
		try {
			String fileKeyStr = getKeyFromFile(ApplicationConstants.LICENSE_KEY_PATH);
			if (StrUtil.isEmpty(fileKeyStr)){
				return null;
			}
			String dateStr = rsaEncryptor.decrypt(base64Encryptor.decrypt(fileKeyStr));
			LocalDateTime keyDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(ApplicationConstants.FORMAT_DATE_TIME));
			return keyDateTime;
		} catch (Exception e) {
			Exceptions.printException(e);
		}
		return null;
	}

	private LocalDate getLicenseDate(){
		try {
			String fileKeyStr = getKeyFromFile(ApplicationConstants.LICENSE_KEY_PATH);
			if (StrUtil.isEmpty(fileKeyStr)){
				return null;
			}
			String dateStr = rsaEncryptor.decrypt(base64Encryptor.decrypt(fileKeyStr));
			LocalDate keyDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(ApplicationConstants.FORMAT_DATE));
			return keyDate;
		} catch (Exception e) {
			Exceptions.printException(e);
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private String getKeyFromFile(String filePath) {
		try {
			//String bufferedReader = BootAppFileReader.getClasspathFileToString(filePath);
			BufferedReader bufferedReader = BootAppFileReader.getClasspathFileJar(filePath);
			String line = null;
			List<String> list = new ArrayList<String>();
			while ((line = bufferedReader.readLine()) != null) {
				list.add(line);
			}
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(list.get(0));
			return stringBuilder.toString();
			//return bufferedReader;
		}catch (Exception e){
		    Exceptions.printException( e );
		    return null;
		}
    }

	public static void main(String[] args) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(ApplicationConstants.FORMAT_DATE_TIME);
		LocalDateTime a = LocalDateTime.parse("9999-12-30 10:30:10", dateTimeFormatter);
		LocalDateTime b = LocalDateTime.now();
		System.out.println("a=====>" + a);
		System.out.println("b=====>" + b);
		boolean flag = DateUtil.localDateTimeIsBefore(a,b);
		System.out.println( flag );
	}
}
