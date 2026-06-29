import { Settings } from 'lucide-react';

export default function SettingsPage() {
  return (
    <div>
      <div className="flex items-center gap-2 mb-6">
        <Settings className="w-6 h-6" />
        <h2 className="text-xl font-bold text-gray-800">系统设置</h2>
      </div>
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <p className="text-gray-500">系统设置功能开发中...</p>
        <div className="mt-4 grid grid-cols-2 gap-4 text-sm text-gray-600">
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="font-medium text-gray-800">后端状态</p>
            <p className="text-green-600 mt-1">运行中 (localhost:18080)</p>
          </div>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="font-medium text-gray-800">数据库</p>
            <p className="text-green-600 mt-1">H2 内存数据库</p>
          </div>
        </div>
      </div>
    </div>
  );
}
