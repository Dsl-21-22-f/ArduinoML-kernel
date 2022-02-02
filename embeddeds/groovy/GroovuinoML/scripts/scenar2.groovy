sensor "button" onPin 9
sensor "button2" pin 12
actuator "buzzer" pin 11
actuator "led" pin 10

state "onBuzzerLed" means "buzzer" becomes "high" and "led" becomes "high"
state "offBuzzerLed" means "buzzer" becomes "low" and "led" becomes "low"

initial "offBuzzerLed"

from "offBuzzerLed" to "onBuzzer" when "button" becomes "down" or "button2" becomes "down"
from "onBuzzerLed" to "offBuzzer" when "button" becomes "up" and "button2" becomes "up"


export "Switch!"