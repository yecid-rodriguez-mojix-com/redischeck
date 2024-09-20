gradle gatlingRun --simulation=towbook.LocationSuiteSimulation  -DbaseUrl=$ENV_BASE_URL -DapiToken=$ENV_API_TOKEN -DrampTo=$ENV_RAMP_TO -DrampTime=$ENV_RAMP_TIME -DconcurrentUsers=$ENV_CONCURRENT_USERS -DconcurrentTime=$ENV_CONCURRENT_TIME
#gradle gatlingRun --simulation=towbook.LocationSuiteSimulation  -DbaseUrl=$ENV_BASE_URL -DapiToken=$ENV_API_TOKEN
