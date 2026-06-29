import { useEffect, useState } from 'react';
import { Users } from 'lucide-react';
import { ownerApi } from '../api';

interface Owner {
  id: number;
  nickname: string;
  phone: string;
  address: string;
  memberLevel: string;
  totalOrders: number;
  totalSpent: number;
}

const levelMap: Record<string, { label: string; color: string }> = {
  NORMAL: { label: '普通', color: 'bg-gray-100 text-gray-700' },
  VIP: { label: 'VIP', color: 'bg-purple-100 text-purple-700' },
  SVIP: { label: 'SVIP', color: 'bg-orange-100 text-orange-700' },
};

export default function OwnersPage() {
  const [owners, setOwners] = useState<Owner[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    ownerApi.list()
      .then((res) => setOwners((res as { data: Owner[] }).data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-gray-400">加载中...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <Users className="w-6 h-6" /> 宠物主人
        </h2>
        <span className="text-sm text-gray-400">{owners.length} 位主人</span>
      </div>
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left font-medium text-gray-500">昵称</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">手机号</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">地址</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">等级</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">订单数</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">累计消费</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {owners.map((o) => {
              const lv = levelMap[o.memberLevel] || { label: o.memberLevel, color: 'bg-gray-100 text-gray-700' };
              return (
                <tr key={o.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-medium text-gray-800">{o.nickname}</td>
                  <td className="px-6 py-4 text-gray-600">{o.phone}</td>
                  <td className="px-6 py-4 text-gray-600 max-w-xs truncate">{o.address || '-'}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${lv.color}`}>
                      {lv.label}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-600">{o.totalOrders}</td>
                  <td className="px-6 py-4 text-gray-600">¥{o.totalSpent}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {owners.length === 0 && (
          <div className="text-center py-12 text-gray-400">暂无宠物主人数据</div>
        )}
      </div>
    </div>
  );
}
