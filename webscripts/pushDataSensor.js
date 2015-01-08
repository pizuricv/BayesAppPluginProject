// sensors should never throw exceptions but instead send an error back

var runtimeParam = options.rawData.GLOBAL[options.requiredProperties.param];
if(!runtimeParam)
    runtimetParam = 0;
value = {
    observedState: runtimeParam > 0 ? "hello" : "world",
    rawData: { runtimeParam: runtimeParam}
};
send(null, value);
