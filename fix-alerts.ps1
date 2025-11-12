$content = Get-Content 'c:\Users\nghip\Desktop\CSDLPT_Demo\QlDoiBong\src\main\java\org\football\controllers\QueryController.java' -Raw
$content = $content -replace 'AlertUtils\.showWarning\("([^"]+)", "([^"]+)"\)', 'AlertUtils.showWarning("$2")'
$content = $content -replace 'AlertUtils\.showError\("([^"]+)", "([^"]+)"\)', 'AlertUtils.showError("$2")'
$content = $content -replace 'AlertUtils\.showInfo\("([^"]+)", "([^"]+)"\)', 'AlertUtils.showInfo("$2")'
$content | Set-Content 'c:\Users\nghip\Desktop\CSDLPT_Demo\QlDoiBong\src\main\java\org\football\controllers\QueryController.java'
Write-Host "✅ Fixed all AlertUtils calls!"
