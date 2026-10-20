/* HCM-UTE - script dùng chung */
(function () {
  'use strict';

  // Mở/đóng sidebar trên màn hình nhỏ
  var burger = document.getElementById('sidebar-toggle');
  var backdrop = document.getElementById('sidebar-backdrop');
  if (burger) {
    burger.addEventListener('click', function () { document.body.classList.toggle('sidebar-open'); });
  }
  if (backdrop) {
    backdrop.addEventListener('click', function () { document.body.classList.remove('sidebar-open'); });
  }

  // Tabs: <div data-tabs><button class="tab" data-target="panel-id">
  document.querySelectorAll('[data-tabs]').forEach(function (tabs) {
    var buttons = tabs.querySelectorAll('.tab');
    buttons.forEach(function (btn) {
      btn.addEventListener('click', function () {
        buttons.forEach(function (b) {
          b.classList.remove('active');
          var p = document.getElementById(b.getAttribute('data-target'));
          if (p) { p.classList.remove('active'); }
        });
        btn.classList.add('active');
        var panel = document.getElementById(btn.getAttribute('data-target'));
        if (panel) { panel.classList.add('active'); }
      });
    });
  });

  // Bấm vào hàng có data-detail để xem/ẩn nội dung chi tiết
  document.querySelectorAll('tr[data-detail]').forEach(function (row) {
    row.addEventListener('click', function (e) {
      e.preventDefault();
      var detail = document.getElementById(row.getAttribute('data-detail'));
      if (detail) {
        detail.hidden = !detail.hidden;
        row.classList.toggle('open', !detail.hidden);
      }
    });
  });

  // Hỏi xác nhận: <form data-confirm="Bạn chắc chắn?">
  document.addEventListener('submit', function (e) {
    var msg = e.target && e.target.getAttribute && e.target.getAttribute('data-confirm');
    if (msg && !window.confirm(msg)) { e.preventDefault(); }
  });

  // Tự ẩn thông báo thành công sau 6 giây
  document.querySelectorAll('.alert-success[data-autoclose]').forEach(function (el) {
    setTimeout(function () { el.style.display = 'none'; }, 6000);
  });

  // Trang đăng nhập: bấm tài khoản demo để điền sẵn
  document.querySelectorAll('[data-fill-user]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var u = document.getElementById('username');
      var p = document.getElementById('password');
      if (u && p) {
        u.value = btn.getAttribute('data-fill-user');
        p.value = btn.getAttribute('data-fill-pass') || '123456';
        p.focus();
      }
    });
  });
})();
