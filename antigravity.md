setInterval(() => {
    const buttons = Array.from(document.querySelectorAll('button'));
    const acceptBtn = buttons.find(b => b.textContent.includes('Accept') || b.textContent.includes('Run') || b.textContent.includes('Always Allow') || b.textContent.includes('Always Allow'));
    if(acceptBtn) acceptBtn.click();
}, 1500);