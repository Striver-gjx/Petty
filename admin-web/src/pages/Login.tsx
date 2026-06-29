import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api';

export default function Login() {
  const [phone, setPhone] = useState('13800000000');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await authApi.login(phone, 'ADMIN');
      const data = (res as { data: { token: string } }).data;
      localStorage.setItem('admin_token', data.token);
      navigate('/', { replace: true });
    } catch (err) {
      alert('登录失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
    setLoading(false);
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 to-orange-100 flex items-center justify-center">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-orange-500">Petty Admin</h1>
          <p className="text-gray-400 mt-2">宠物上门喂养管理后台</p>
        </div>
        <form onSubmit={handleLogin}>
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">手机号</label>
            <input
              type="text"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-orange-300 focus:border-orange-400 outline-none"
              placeholder="请输入管理员手机号"
            />
          </div>
          <button
            type="submit"
            disabled={loading || phone.length !== 11}
            className="w-full py-3 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600 disabled:opacity-50 transition-colors"
          >
            {loading ? '登录中...' : '登录'}
          </button>
          <p className="text-center text-xs text-gray-400 mt-4">
            演示账号: 13800000000（管理员）
          </p>
        </form>
      </div>
    </div>
  );
}
