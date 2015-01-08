if(options.globalSettings.MANDRILL_KEY === undefined || options.requiredProperties.message === undefined ||
    options.requiredProperties.subject === undefined || options.requiredProperties.from === undefined || 
    options.requiredProperties.to === undefined){
        send(new Error("Error missing properties"));
    }
else {
    var tagLine = "\n\n\n -------------------------------\npowered by waylay.io";
    var message = {
    key: options.globalSettings.MANDRILL_KEY,
    message: {
    text: options.requiredProperties.message + tagLine,
    subject: options.requiredProperties.subject,
    from_email: options.requiredProperties.from,
    from_name: options.requiredProperties.from,
    to: [
        {
            email: options.requiredProperties.to,
            name: options.requiredProperties.to,
            type: "to"
        }
    ],
    headers: {
        "Reply-To": options.requiredProperties.from
    },
    important: false,
    track_opens: null,
    track_clicks: null,
    auto_text: null,
    auto_html: null,
    inline_css: null,
    url_strip_qs: null,
    preserve_recipients: null,
    view_content_link: null,
    tracking_domain: null,
    signing_domain: null,
    return_path_domain: null,
    merge: true,
    merge_language: "mailchimp",
    send_at: "2000-01-01",
    tags: [
        "support"
    ]
    },
    async: false,
    ip_pool: "Main Pool"
    };
          
    var options = {
      url: 'https://mandrillapp.com/api/1.0/messages/send.json"',
      json: message
    };
    
    request.post(options, function (error, response, body) {
      if (!error && response.statusCode == 200) {
        send();
      }else{
          send(new Error("Error " + error + " " + body));
      }
    });
}

      
