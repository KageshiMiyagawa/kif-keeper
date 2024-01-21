window.onload = function() {
	var dialogEvent = document.getElementById('dialogEvent');
	if (dialogEvent) {
		var dialogText = dialogEvent.getAttribute('dialogText');
		if (dialogText != null) {
			dispCommonDialog(dialogText);
		}
	}
};

function openSearchForm() {
	var searchForm = document.getElementById("searchForm");
	searchForm.style.display = "block";
	var kifuListMenu = document.getElementById("kifuListMenu");
	kifuListMenu.style.display = "none";
}

function closeSearchForm() {
	var searchForm = document.getElementById("searchForm");
	searchForm.style.display = "none";
	var kifuListMenu = document.getElementById("kifuListMenu");
	kifuListMenu.style.display = "flex";
}

function openRegistForm() {
	var registForm = document.getElementById("registForm");
	registForm.style.display = "block";
	var settingMenu = document.getElementById("settingMenu");
	settingMenu.style.display = "none";
}

function closeRegistForm() {
	var registForm = document.getElementById("registForm");
	registForm.style.display = "none";
	var settingMenu = document.getElementById("settingMenu");
	settingMenu.style.display = "block";
}

function dispChangeSettingForm(appType, changeType) {
	var menuElement = null;
	var formElement = null;
	if(appType == "wars") {
		menuElement = document.getElementById("wars_setting_menu");
		formElement = document.getElementById("wars_setting_form");
	} else if (appType == "club24") {
		menuElement = document.getElementById("club24_setting_menu");
		formElement = document.getElementById("club24_setting_form");
	} else if (appType == "quest") {
		menuElement = document.getElementById("quest_setting_menu");
		formElement = document.getElementById("quest_setting_form");
	}
	
	if(changeType == "menu") {
		menuElement.style.display = "none";
		formElement.style.display = "block";
	} else {
		menuElement.style.display = "block";
		formElement.style.display = "none";		
	}
}

function kifuCopyHandler(event) {
	var targetId = event.target.id;
	var targetArray = targetId.split("_");
	var kifuId = targetArray[1];
	$.ajax({
	  url: '/kifu/' + kifuId,
	  type: "GET",
	  dataType: "json",
	  success: function(data) {
	    copyToClipboard(data.kifuText);
	  },
	  error: function(jqXHR, textStatus, errorThrown) {
		  console.log(textStatus, errorThrown);
		  console.log(jqXHR.responseText);
	  }
	});
}

function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(function() {
	dispCommonDialog("棋譜をコピーしました！");
  }, function(err) {
    console.error("Failed to copy to clipboard: ", err);
  });
}

function targetDeleteHandler(event) {
	var targetId = event.target.id;
	var targetArray = targetId.split("_");
	var appId;
	var type;
	var appIdTmp = "";
	// 以降のforはIDに_が入っていた場合の考慮
	for(let i = 0; i < targetArray.length; i++) {
		if(i == 0) {
			continue;
		}
		if(i == targetArray.length - 1) {
			type = targetArray[targetArray.length - 1];
			break;
		}
		
		if (appIdTmp.length === 0) {
			appIdTmp = targetArray[i];
		} else {
			appIdTmp = appIdTmp + "_" + targetArray[i];
		}
		if(i == targetArray.length - 2) {
			appId = appIdTmp;
		}
	}
	
	var clearKifu = confirm("棋譜情報も削除しますか？");
	
    var delRowSelector = type + "-" + appId;
    var delRow = document.getElementById(delRowSelector);
    delRow.parentNode.removeChild(delRow);
    alert(appId+"を収集対象から削除しました。");
    
	$.ajax({
	  url: '/setting/' + appId,
	  type: "POST",
	  dataType: "json",
	  data: {
		  appType: type,
		  clearKifu: clearKifu
	  },
	  success: function(data) {
	    console.log("削除成功 appType:"+appType+" appId:"+appId);
	  },
	  error: function(jqXHR, textStatus, errorThrown) {
		  console.log(textStatus, errorThrown);
		  console.log(jqXHR.responseText);
	  }
	});
}

function dispCommonDialog(text) {
	var dialog = document.getElementById("com-dialog");
	var dialogMessage = document.getElementById("dialog-message");
	dialogMessage.textContent = text;
	dialog.style.display = "block";
    setTimeout(function() {
		dialog.style.opacity = 0;
		setTimeout(function(){
			dialog.style.display = "none";
			dialog.style.opacity = 1;
		}, 500);
	}, 3000);
}

function clearSearchCondition() {
	var kifuSearchForm = document.getElementById("kifuSearchForm");
	kifuSearchForm.reset();
}

function appTypeChangeHandler() {
	var appTypeSelect = document.getElementById("appTypeSelect");
	var appIdSelect = document.getElementById("appIdSelect");
	
	var selectedOption = appTypeSelect.value;
	var appIdOptions = appIdSelect.options;
	
	var appType;
	if(selectedOption == "1") {
		appType = "将棋ウォーズ";
	} else if (selectedOption == "2") {
		appType = "将棋倶楽部24";
	} 
//	else if (appType == "3") {
//		appType = "将棋クエスト";
//	}
	
	if (selectedOption.trim().length === 0) {
		// 空白選択時はオプションをすべて表示
		Array.from(appIdOptions).forEach(function(option) {
			option.style.display = ""; 
		});
		return;
	}
	
	Array.from(appIdOptions).forEach(function(option) {
		// 選択内容に該当するオプションのみ表示
		if(option.textContent.includes(appType)) {
			option.style.display = ""; 
		} else {
			option.style.display = "none";
		}
		
	});
}