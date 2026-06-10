// ai-assistant.js
Page({
  data: { webviewUrl: '' },

  onLoad() {
    var integrateId = 'cit-176c651331b04fa6922a';
    var requestDomain = 'https://1557976991754980.appflow.aliyunnest.com';
    var baseUrl = 'http://10.17.86.214:8084';
    try {
      var app = getApp();
      if (app && app.globalData && app.globalData.baseUrl) baseUrl = app.globalData.baseUrl;
    } catch(e) {}

    var url = baseUrl + '/chatbot.html?integrateId=' + encodeURIComponent(integrateId) +
              '&requestDomain=' + encodeURIComponent(requestDomain);
    this.setData({ webviewUrl: url });
  }
});
