import { useEffect, useState } from 'react';
import { UserCheck, Star } from 'lucide-react';
import { sitterApi } from '../api';

interface Sitter {
  id: number;
  name: string;
  phone: string;
  serviceArea: string;
  rating: number;
  totalOrders: number;
  status: string;
  bio: string;
  experienceYears: number;
  basePrice: number;
}

const statusMap: Record<string, { label: string; color: string }> = {
  ACTIVE: { label: '在线', color: 'bg-green-100 text-green-700' },
  PENDING_REVIEW: { label: '待审核', color: 'bg-yellow-100 text-yellow-700' },
  SUSPENDED: { label: '暂停', color: 'bg-red-100 text-red-700' },
  OFFLINE: { label: '离线', color: 'bg-gray-100 text-gray-700' },
  BANNED: { label: '封禁', color: 'bg-red-200 text-red-800' },
};

export default function SittersPage() {
  const [sitters, setSitters] = useState<Sitter[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    sitterApi.list()
      .then((res) => setSitters((res as { data: Sitter[] }).data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-gray-400">加载中...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <UserCheck className="w-6 h-6" /> 喂养师管理
        </h2>
        <span className="text-sm text-gray-400">{sitters.length} 位喂养师</span>
      </div>
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left font-medium text-gray-500">姓名</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">手机号</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">服务区域</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">评分</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">服务次数</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">基础报价</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">状态</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {sitters.map((s) => {
              const st = statusMap[s.status] || { label: s.status, color: 'bg-gray-100 text-gray-700' };
              return (
                <tr key={s.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-medium text-gray-800">{s.name}</td>
                  <td className="px-6 py-4 text-gray-600">{s.phone}</td>
                  <td className="px-6 py-4 text-gray-600">{s.serviceArea || '-'}</td>
                  <td className="px-6 py-4">
                    <span className="flex items-center gap-1">
                      <Star className="w-4 h-4 text-yellow-400 fill-yellow-400" />
                      {s.rating}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-600">{s.totalOrders}</td>
                  <td className="px-6 py-4 text-gray-600">¥{s.basePrice}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${st.color}`}>
                      {st.label}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {sitters.length === 0 && (
          <div className="text-center py-12 text-gray-400">暂无喂养师数据</div>
        )}
      </div>
    </div>
  );
}
