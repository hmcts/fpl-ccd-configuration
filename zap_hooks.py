def zap_started(zap, target):
  rules_to_ignore = ('90033', '10023', '10010', '10054')
  for rule in rules_to_ignore:
    zap.pscan.set_scanner_alert_threshold(id=rule, alertthreshold='OFF')
