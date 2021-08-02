import time

def zap_started(zap, target):
  rules_to_ignore = ('90033', '10023', '10010', '10054')
  for rule in rules_to_ignore:
    zap.pscan.set_scanner_alert_threshold(id=rule, alertthreshold='OFF')

def zap_pre_shutdown(zap):
  print("zap_pre_shutdown: Leave ZAP server up for report calls...".format(context_id))
  time.sleep(20)
  print("zap_pre_shutdown: Continue shut down.".format(context_id))
