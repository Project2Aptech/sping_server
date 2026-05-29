package org.example.spring_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.UserDTO;
import org.example.spring_server.service.PasswordResetService;
import org.example.spring_server.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody UserDTO.ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> resetPasswordPage(@RequestParam String token) {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <title>Reset Password — SportT5</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }

                    body {
                        font-family: 'Segoe UI', sans-serif;
                        background: #121212;
                        color: #fff;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }

                    .card {
                        background: #1a1a1a;
                        border-radius: 16px;
                        padding: 48px 40px;
                        width: 100%%;
                        max-width: 420px;
                        box-shadow: 0 8px 32px rgba(0,0,0,0.4);
                    }

                    .logo {
                        text-align: center;
                        margin-bottom: 32px;
                    }

                    .logo span {
                        font-size: 28px;
                        font-weight: 800;
                        color: #1DB954;
                        letter-spacing: -1px;
                    }

                    h2 {
                        text-align: center;
                        font-size: 22px;
                        font-weight: 700;
                        margin-bottom: 8px;
                        color: #fff;
                    }

                    .subtitle {
                        text-align: center;
                        color: #aaa;
                        font-size: 14px;
                        margin-bottom: 32px;
                    }

                    .form-group {
                        margin-bottom: 20px;
                    }

                    label {
                        display: block;
                        font-size: 13px;
                        font-weight: 600;
                        color: #ccc;
                        margin-bottom: 8px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                    }

                    .input-wrapper {
                        position: relative;
                    }

                    input[type="password"] {
                        width: 100%%;
                        padding: 14px 44px 14px 16px;
                        background: #2a2a2a;
                        border: 1.5px solid #333;
                        border-radius: 8px;
                        color: #fff;
                        font-size: 15px;
                        outline: none;
                        transition: border-color 0.2s;
                    }

                    input[type="password"]:focus {
                        border-color: #1DB954;
                    }

                    .toggle-password {
                        position: absolute;
                        right: 14px;
                        top: 50%%;
                        transform: translateY(-50%%);
                        cursor: pointer;
                        color: #888;
                        font-size: 18px;
                        user-select: none;
                    }

                    .toggle-password:hover { color: #fff; }

                    .strength-bar {
                        height: 4px;
                        border-radius: 2px;
                        background: #333;
                        margin-top: 8px;
                        overflow: hidden;
                    }

                    .strength-fill {
                        height: 100%%;
                        width: 0%%;
                        border-radius: 2px;
                        transition: width 0.3s, background 0.3s;
                    }

                    .strength-text {
                        font-size: 12px;
                        margin-top: 4px;
                        color: #888;
                    }

                    .requirements {
                        margin-top: 12px;
                        padding: 12px;
                        background: #222;
                        border-radius: 8px;
                        font-size: 12px;
                        color: #888;
                    }

                    .req {
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        margin-bottom: 4px;
                    }

                    .req:last-child { margin-bottom: 0; }

                    .req-icon { font-size: 11px; }
                    .req.met { color: #1DB954; }
                    .req.unmet { color: #888; }

                    .error-msg {
                        background: #3a1a1a;
                        border: 1px solid #c0392b;
                        border-radius: 8px;
                        padding: 12px 16px;
                        font-size: 13px;
                        color: #e74c3c;
                        margin-bottom: 20px;
                        display: none;
                    }

                    button[type="submit"] {
                        width: 100%%;
                        padding: 15px;
                        background: #1DB954;
                        color: #000;
                        border: none;
                        border-radius: 50px;
                        font-size: 15px;
                        font-weight: 700;
                        cursor: pointer;
                        margin-top: 8px;
                        transition: background 0.2s, transform 0.1s;
                        letter-spacing: 0.3px;
                    }

                    button[type="submit"]:hover { background: #1ed760; }
                    button[type="submit"]:active { transform: scale(0.98); }
                    button[type="submit"]:disabled {
                        background: #333;
                        color: #666;
                        cursor: not-allowed;
                    }

                    .spinner {
                        display: none;
                        width: 18px;
                        height: 18px;
                        border: 2px solid #000;
                        border-top-color: transparent;
                        border-radius: 50%%;
                        animation: spin 0.7s linear infinite;
                        margin: 0 auto;
                    }

                    @keyframes spin { to { transform: rotate(360deg); } }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="logo"><span>SportT5</span></div>
                    <h2>Set new password</h2>
                    <p class="subtitle">Must be at least 6 characters long</p>

                    <div class="error-msg" id="errorMsg"></div>

                    <form id="resetForm" method="POST" action="/api/v1/auth/reset-password-form">
                        <input type="hidden" name="token" value="%s"/>

                        <div class="form-group">
                            <label>New Password</label>
                            <div class="input-wrapper">
                                <input type="password" id="newPassword" name="newPassword"
                                       placeholder="Enter new password" required minlength="6"/>
                                <span class="toggle-password" onclick="toggleVisibility('newPassword', this)">👁</span>
                            </div>
                            <div class="strength-bar">
                                <div class="strength-fill" id="strengthFill"></div>
                            </div>
                            <div class="strength-text" id="strengthText"></div>
                            <div class="requirements">
                                <div class="req unmet" id="req-length">
                                    <span class="req-icon">○</span> At least 6 characters
                                </div>
                                <div class="req unmet" id="req-upper">
                                    <span class="req-icon">○</span> One uppercase letter
                                </div>
                                <div class="req unmet" id="req-number">
                                    <span class="req-icon">○</span> One number
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Confirm Password</label>
                            <div class="input-wrapper">
                                <input type="password" id="confirmPassword"
                                       placeholder="Confirm new password" required/>
                                <span class="toggle-password" onclick="toggleVisibility('confirmPassword', this)">👁</span>
                            </div>
                        </div>

                        <button type="submit" id="submitBtn">
                            <span id="btnText">Reset Password</span>
                            <div class="spinner" id="spinner"></div>
                        </button>
                    </form>
                </div>

                <script>
                    const passwordInput = document.getElementById('newPassword');
                    const confirmInput  = document.getElementById('confirmPassword');
                    const strengthFill  = document.getElementById('strengthFill');
                    const strengthText  = document.getElementById('strengthText');
                    const submitBtn     = document.getElementById('submitBtn');
                    const errorMsg      = document.getElementById('errorMsg');

                    function toggleVisibility(inputId, icon) {
                        const input = document.getElementById(inputId);
                        if (input.type === 'password') {
                            input.type = 'text';
                            icon.textContent = '🙈';
                        } else {
                            input.type = 'password';
                            icon.textContent = '👁';
                        }
                    }

                    function updateReq(id, met) {
                        const el = document.getElementById(id);
                        el.className = 'req ' + (met ? 'met' : 'unmet');
                        el.querySelector('.req-icon').textContent = met ? '✓' : '○';
                    }

                    passwordInput.addEventListener('input', () => {
                        const val = passwordInput.value;
                        const hasLength = val.length >= 6;
                        const hasUpper  = /[A-Z]/.test(val);
                        const hasNumber = /[0-9]/.test(val);

                        updateReq('req-length', hasLength);
                        updateReq('req-upper',  hasUpper);
                        updateReq('req-number', hasNumber);

                        const score = [hasLength, hasUpper, hasNumber].filter(Boolean).length;
                        const colors = ['#e74c3c', '#e67e22', '#1DB954'];
                        const labels = ['Weak', 'Fair', 'Strong'];
                        const widths = ['33%%', '66%%', '100%%'];

                        if (val.length === 0) {
                            strengthFill.style.width = '0';
                            strengthText.textContent = '';
                        } else {
                            strengthFill.style.width   = widths[score - 1] || '10%%';
                            strengthFill.style.background = colors[score - 1] || '#e74c3c';
                            strengthText.textContent   = labels[score - 1] || 'Too short';
                            strengthText.style.color   = colors[score - 1] || '#e74c3c';
                        }
                    });

                    document.getElementById('resetForm').addEventListener('submit', function(e) {
                        errorMsg.style.display = 'none';

                        if (passwordInput.value !== confirmInput.value) {
                            e.preventDefault();
                            errorMsg.textContent = 'Passwords do not match.';
                            errorMsg.style.display = 'block';
                            return;
                        }

                        if (passwordInput.value.length < 6) {
                            e.preventDefault();
                            errorMsg.textContent = 'Password must be at least 6 characters.';
                            errorMsg.style.display = 'block';
                            return;
                        }

                        submitBtn.disabled = true;
                        document.getElementById('btnText').style.display = 'none';
                        document.getElementById('spinner').style.display = 'block';
                    });
                </script>
            </body>
            </html>
            """.formatted(token);

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping("/reset-password-form")
    public ResponseEntity<String> resetPasswordForm(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            passwordResetService.resetPassword(
                    new UserDTO.ResetPasswordRequest(token, newPassword));

            String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8"/>
                    <title>Password Reset — SportT5</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body {
                            font-family: 'Segoe UI', sans-serif;
                            background: #121212;
                            color: #fff;
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .card {
                            background: #1a1a1a;
                            border-radius: 16px;
                            padding: 48px 40px;
                            width: 100%%;
                            max-width: 420px;
                            text-align: center;
                            box-shadow: 0 8px 32px rgba(0,0,0,0.4);
                        }
                        .icon { font-size: 56px; margin-bottom: 24px; }
                        .logo { font-size: 24px; font-weight: 800; color: #1DB954; margin-bottom: 24px; }
                        h2 { font-size: 22px; font-weight: 700; margin-bottom: 12px; }
                        p { color: #aaa; font-size: 14px; line-height: 1.6; margin-bottom: 32px; }
                        .btn {
                            display: inline-block;
                            padding: 14px 36px;
                            background: #1DB954;
                            color: #000;
                            border-radius: 50px;
                            font-weight: 700;
                            font-size: 14px;
                            text-decoration: none;
                            transition: background 0.2s;
                        }
                        .btn:hover { background: #1ed760; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="logo">SportT5</div>
                        <div class="icon">✅</div>
                        <h2>Password reset!</h2>
                        <p>Your password has been updated successfully.<br/>
                           You can now log in with your new password.</p>
                    </div>
                </body>
                </html>
                """;

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .body(html);

        } catch (Exception e) {
            String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8"/>
                    <title>Error — SportT5</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body {
                            font-family: 'Segoe UI', sans-serif;
                            background: #121212;
                            color: #fff;
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .card {
                            background: #1a1a1a;
                            border-radius: 16px;
                            padding: 48px 40px;
                            width: 100%%;
                            max-width: 420px;
                            text-align: center;
                            box-shadow: 0 8px 32px rgba(0,0,0,0.4);
                        }
                        .icon { font-size: 56px; margin-bottom: 24px; }
                        .logo { font-size: 24px; font-weight: 800; color: #1DB954; margin-bottom: 24px; }
                        h2 { font-size: 22px; font-weight: 700; margin-bottom: 12px; }
                        p { color: #aaa; font-size: 14px; line-height: 1.6; margin-bottom: 32px; }
                        .btn {
                            display: inline-block;
                            padding: 14px 36px;
                            background: #e74c3c;
                            color: #fff;
                            border-radius: 50px;
                            font-weight: 700;
                            font-size: 14px;
                            text-decoration: none;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="logo">SportT5</div>
                        <div class="icon">❌</div>
                        <h2>Link expired</h2>
                        <p>This password reset link is invalid or has expired.<br/>
                           Please request a new one.</p>
                    </div>
                </body>
                </html>
                """;

            return ResponseEntity.badRequest()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .body(html);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO.AuthResponse> register(
            @Valid @RequestBody UserDTO.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO.AuthResponse> login(
            @Valid @RequestBody UserDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}