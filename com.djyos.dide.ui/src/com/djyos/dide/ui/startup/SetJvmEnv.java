package com.djyos.dide.ui.startup;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.stream.Stream;

//import org.apache.commons.lang3.SystemUtils;

public class SetJvmEnv {

	// D:\Program Files\SlickEdit Pro 22.0.0\win\;D:\DIDE\IDE\yagarto-gcc4.9\bin;
	// D:\Program Files (x86)\C-Sky Development Suite\cygwin\bin;
	// D:\Program Files (x86)\C-Sky Development Suite\cygwin\csky-elf-tools\bin;
	// C:\ProgramData\Oracle\Java\javapath;%SystemRoot%\system32;%SystemRoot%;
	// %SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\;
	// %JAVA_HOME%\bin;%JAVA_HOME%\jre\bin;D:\Program Files\TortoiseSVN\bin;
	@SuppressWarnings("unchecked")
	public static <K, V> void setenv(final String key, final String value) {
//		try {
//			/// we obtain the actual environment
//			final Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
//			final Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
//			final boolean environmentAccessibility = theEnvironmentField.isAccessible();
//			theEnvironmentField.setAccessible(true);
//
//			final Map<K, V> env = (Map<K, V>) theEnvironmentField.get(null);
//
//			if (SystemUtils.IS_OS_WINDOWS) {
//				// This is all that is needed on windows running java jdk 1.8.0_92
//				if (value == null) {
//					env.remove(key);
//				} else {
//					env.put((K) key, (V) value);
//				}
//			} else {
//				// This is triggered to work on openjdk 1.8.0_91
//				// The ProcessEnvironment$Variable is the key of the map
//				final Class<K> variableClass = (Class<K>) Class.forName("java.lang.ProcessEnvironment$Variable");
//				final Method convertToVariable = variableClass.getMethod("valueOf", String.class);
//				final boolean conversionVariableAccessibility = convertToVariable.isAccessible();
//				convertToVariable.setAccessible(true);
//
//				// The ProcessEnvironment$Value is the value fo the map
//				final Class<V> valueClass = (Class<V>) Class.forName("java.lang.ProcessEnvironment$Value");
//				final Method convertToValue = valueClass.getMethod("valueOf", String.class);
//				final boolean conversionValueAccessibility = convertToValue.isAccessible();
//				convertToValue.setAccessible(true);
//
//				if (value == null) {
//					env.remove(convertToVariable.invoke(null, key));
//				} else {
//					// we place the new value inside the map after conversion so as to
//					// avoid class cast exceptions when rerunning this code
//					env.put((K) convertToVariable.invoke(null, key), (V) convertToValue.invoke(null, value));
//
//					// reset accessibility to what they were
//					convertToValue.setAccessible(conversionValueAccessibility);
//					convertToVariable.setAccessible(conversionVariableAccessibility);
//				}
//			}
//			// reset environment accessibility
//			theEnvironmentField.setAccessible(environmentAccessibility);
//
//			// we apply the same to the case insensitive environment
//			final Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
//					.getDeclaredField("theCaseInsensitiveEnvironment");
//			final boolean insensitiveAccessibility = theCaseInsensitiveEnvironmentField.isAccessible();
//			theCaseInsensitiveEnvironmentField.setAccessible(true);
//			// Not entirely sure if this needs to be casted to ProcessEnvironment$Variable
//			// and $Value as well
//			final Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
//			if (value == null) {
//				// remove if null
//				cienv.remove(key);
//			} else {
//				cienv.put(key, value);
//			}
//			theCaseInsensitiveEnvironmentField.setAccessible(insensitiveAccessibility);
//		} catch (final ClassNotFoundException e) {
//			throw new IllegalStateException("Failed setting environment variable <" + key + "> to <" + value + ">", e);
//		} catch (final NoSuchMethodException e) {
//			throw new IllegalStateException("Failed setting environment variable <" + key + "> to <" + value + ">", e);
//		} catch (final IllegalAccessException e) {
//			throw new IllegalStateException("Failed setting environment variable <" + key + "> to <" + value + ">", e);
//		}  catch (final InvocationTargetException e) {
//			throw new IllegalStateException("Failed setting environment variable <" + key + "> to <" + value + ">", e);
//		} catch (final NoSuchFieldException e) {
//			// we could not find theEnvironment
//			final Map<String, String> env = System.getenv();
//			Stream.of(Collections.class.getDeclaredClasses())
//					// obtain the declared classes of type $UnmodifiableMap
//					.filter(c1 -> "java.util.Collections$UnmodifiableMap".equals(c1.getName())).map(c1 -> {
//						try {
//							return c1.getDeclaredField("m");
//						} catch (final NoSuchFieldException e1) {
//							throw new IllegalStateException("Failed setting environment variable <" + key + "> to <"
//									+ value + "> when locating in-class memory map of environment", e1);
//						}
//					}).forEach(field -> {
//						try {
//							final boolean fieldAccessibility = field.isAccessible();
//							field.setAccessible(true);
//							// we obtain the environment
//							final Map<String, String> map = (Map<String, String>) field.get(env);
//							if (value == null) {
//								// remove if null
//								map.remove(key);
//							} else {
//								map.put(key, value);
//							}
//							// reset accessibility
//							field.setAccessible(fieldAccessibility);
//						} catch (final ConcurrentModificationException e1) {
//							// This may happen if we keep backups of the environment before calling this
//							// method
//							// as the map that we kept as a backup may be picked up inside this block.
//							// So we simply skip this attempt and continue adjusting the other maps
//							// To avoid this one should always keep individual keys/value backups not the
//							// entire map
//							System.out.printf("Attempted to modify source map: " + field.getDeclaringClass() + "#"
//									+ field.getName(), e1);
//						} catch (final IllegalAccessException e1) {
//							throw new IllegalStateException("Failed setting environment variable <" + key + "> to <"
//									+ value + ">. Unable to access field!", e1);
//						}
//					});
//		}
//		System.out.printf(
//				"Set environment variable <" + key + "> to <" + value + ">. Sanity Check: " + System.getenv(key));
	}

}
