package org.zzt.note.boot.nativeimage;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(DruidRuntimeHints.class)
public class NativeImageHintsConfiguration {
}

