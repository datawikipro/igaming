#!/usr/bin/env pwsh
$patch = @'
[{"op":"replace","path":"/spec/template/spec/containers/0/resources/requests/cpu","value":"100m"}]
'@
kubectl patch deployment igaming-portal -n igaming-dev --type=json -p $patch
