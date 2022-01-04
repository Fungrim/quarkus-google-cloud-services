package io.quarkiverse.googlecloudservices.logging.deployment;

import io.quarkiverse.googlecloudservices.logging.runtime.LoggingConfiguration;
import io.quarkiverse.googlecloudservices.logging.runtime.cdi.LoggingProducer;
import io.quarkiverse.googlecloudservices.logging.runtime.cdi.WriteOptionsProducer;
import io.quarkiverse.googlecloudservices.logging.runtime.recorder.LoggingHandlerFactory;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LogHandlerBuildItem;

public class LoggingBuildSteps {

    private static final String FEATURE = "google-cloud-logging";

    @BuildStep
    public FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem logging() {
        return new AdditionalBeanBuildItem(LoggingProducer.class);
    }

    @BuildStep
    public AdditionalBeanBuildItem writeOptions() {
        return new AdditionalBeanBuildItem(WriteOptionsProducer.class);
    }

    /*@BuildStep
    public UnremovableBeanBuildItem credentials() {
        return UnremovableBeanBuildItem.beanTypes(GoogleCredentials.class);
    }

    @BuildStep
    public UnremovableBeanBuildItem helperClasses() {
        return UnremovableBeanBuildItem.beanTypes(
                EscJsonFormat.class,
                LevelTransformer.class,
                JsonFormatter.class,
                LabelExtractor.class,
                LoggingConfiguration.class,
                TraceInfoExtractor.class,
                TraceInfo.class,
                JsonHandler.class,
                TextHandler.class,
                SimpleFormatter.class,
                StackTraceArrayRenderer.class);
    }*/

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public LogHandlerBuildItem handler(LoggingConfiguration config, LoggingHandlerFactory factory) {
        return new LogHandlerBuildItem(factory.create(config));
    }
}
