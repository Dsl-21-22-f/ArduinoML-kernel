sensor "buttonOne" onPin 9
sensor "buttonTwo" pin 12
actuator "buzzer" pin 11
actuator "led" pin 10

state "onBuzzerLed" means "buzzer" becomes "high" and "led" becomes "high"
state "offBuzzerLed" means "buzzer" becomes "low" and "led" becomes "low"

initial "offBuzzerLed"

from "offBuzzerLed" to "onBuzzerLed" when "buttonOne" becomes "down" and "buttonTwo" becomes "down"
from "onBuzzerLed" to "offBuzzerLed" when "buttonOne" becomes "up" or "buttonTwo" becomes "up"


export "Switch!"