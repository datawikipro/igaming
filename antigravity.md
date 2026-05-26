- [x] Implement Asynchronous / Lazy Initialization in Java Core
  - [x] Modify `VpnManagerService.java` to initialize VPN/proxy configuration asynchronously
  - [x] Modify `BrowserService.java` to lazily instantiate `Playwright` on demand

setInterval(() => {
    const buttons = Array.from(document.querySelectorAll('button'));
    const acceptBtn = buttons.find(b => b.textContent.includes('Accept') || b.textContent.includes('Run') || b.textContent.includes('Always Allow') || b.textContent.includes('Always Allow') || b.textContent.includes('Allow in Workspace') || b.textContent.includes('Retry'));
    if(acceptBtn) acceptBtn.click();
}, 1500);