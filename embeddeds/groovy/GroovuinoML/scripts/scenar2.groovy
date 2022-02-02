sensor "button" onPin 9
sensor "button2" pin 12
actuator "buzzer" pin 11
actuator "led" pin 10

state "onBuzzer" means "buzzer" becomes "high" and "led" becomes "high"
state "offBuzzer" means "buzzer" becomes "low" and "led" becomes "low"

initial "offBuzzer"

from "offBuzzer" to "onBuzzer" when "button" becomes "down" or "button2" becomes "down"
from "onBuzzer" to "offBuzzer" when "button" becomes "up" and "button2" becomes "up"


export "Switch!"