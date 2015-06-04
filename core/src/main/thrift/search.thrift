include 'common.thrift'

namespace java com.goshoplane.creed.search
namespace js neutrino.search

typedef string JSON
typedef i32 PageIndex
typedef i32 PageSize

exception CreedException {
  1: string message;
}

struct CreedScore {
  1: double value;
}

struct CatalogueSearchId {
  1: common.UserId userId;
  2: i64 sruid;
}

struct CatalogueResultEntry {
  1: common.CatalogueItemId itemId;
  2: CreedScore score;
}

struct CatalogueSearchResults {
  1: CatalogueSearchId searchId;
  2: list<CatalogueResultEntry> results;
}

struct QueryParamWeight {
  1: double value;
}

struct QueryParam {
  1: optional JSON json;
  2: optional binary stream;
  3: optional string value;
  4: optional list<string> values;
  5: optional map<string, string> queryMap;
  6: optional QueryParamWeight weight = {"value" : 1.0};
}

struct CatalogueSearchQuery {
  1: map<string, QueryParam> params;
  2: string queryText;
}

struct CatalogueSearchRequest {
  1: CatalogueSearchId searchId;
  2: CatalogueSearchQuery query;
  3: PageIndex pageIndex;
  4: PageSize pageSize;
}