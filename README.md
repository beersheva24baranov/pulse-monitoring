# Task definition
## Update pulse-values-imitator
### Distribution of the pulse values for each patient should be more less real with consideration following properties
JUMP_PROB - probability of jump, for example 10 percents<br>
MIN_JUMP_PERCENT - minimal percent of jump, for example: 10; means 10% jump, for last value = 100, jump will be  10<br>
MAX_JUMP_PERCENT - maximal percent of jump, for example: 100; means 100% jump, for last value = 100, jump will be  100<br>
JUMP_POSITIVE_PROB - probability of positive jump, for example: 70% means: if last value 100, jump 20% - with probability 70% new value will be 120, with probability 30% - 80
### Testing
introduce constant PATIENT_ID_FOR_INFO_LOGGING, for example 3 <br>
with logger level 'info' to log all generated pulse values for PATIENT_ID_FOR_INFO_LOGGING patient id <br>
DEFAULT_N_PACKETS = 1000 <br>
DEFAULT_N_PATIENTS = 10 <br>
see the distibution of all values for the specified patient id
### Logging
All sent sensor data should be logged under level 'finest'
Only jump data should be logged under level 'debug'
All pulse values for the PATIENT_ID_FOR_INFO_LOGGING should be logged under level 'info'
## Update pulse-values-receiver
### Introduce logging
All received sensor data objects (JSON's) should be logged under level 'finest'
Sensor data containing values greater than 220 should be logged under level 'warning'
Sensor data containing values greater than 230 should be logged under level 'severe'
